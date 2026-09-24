package streamix.server

import java.sql.Connection
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.util.UUID

class SocialRepository(private val db: Database) {
    suspend fun upsertUser(identity: GoogleIdentity): UserDto = db.query { c ->
        c.prepareStatement("""
            INSERT INTO users(uid,email,nickname,avatar_url) VALUES(?,?,?,?)
            ON CONFLICT(uid) DO UPDATE SET email=EXCLUDED.email,avatar_url=COALESCE(EXCLUDED.avatar_url,users.avatar_url),updated_at=NOW()
        """).use {
            it.setString(1,identity.uid); it.setString(2,identity.email)
            it.setString(3,identity.name?.takeIf(String::isNotBlank) ?: "AniLab User"); it.setString(4,identity.picture); it.executeUpdate()
        }
        c.user(identity.uid)!!
    }

    suspend fun profile(uid:String):UserDto?=db.query{it.user(uid)}

    suspend fun updateProfile(uid:String,r:ProfileUpdateRequest):UserDto=db.query{c->
        c.prepareStatement("UPDATE users SET nickname=?,avatar_url=?,bio=?,updated_at=NOW() WHERE uid=?").use{
            it.setString(1,r.nickname.trim().ifBlank{"AniLab User"});it.setString(2,r.avatarUrl);it.setString(3,r.bio);it.setString(4,uid);it.executeUpdate()
        }
        c.user(uid)?:error("User not found")
    }

    suspend fun touch(uid:String)=db.query{c->c.prepareStatement("UPDATE users SET last_seen_at=NOW() WHERE uid=?").use{it.setString(1,uid);it.executeUpdate()}}

    suspend fun friends(uid:String):List<FriendDto>=db.query{c->
        c.prepareStatement("SELECT u.uid,u.nickname,u.avatar_url,f.status FROM friendships f JOIN users u ON u.uid=f.friend_uid WHERE f.user_uid=? ORDER BY u.nickname").use{
            it.setString(1,uid);it.executeQuery().use{rs->buildList{while(rs.next())add(FriendDto(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4)))}}
        }
    }

    suspend fun requestFriend(uid:String,friendUid:String)=db.query{c->
        require(uid!=friendUid){"Cannot add yourself"}
        c.prepareStatement("INSERT INTO friendships(user_uid,friend_uid,status) VALUES(?,?, 'pending') ON CONFLICT(user_uid,friend_uid) DO UPDATE SET status='pending',updated_at=NOW()").use{
            it.setString(1,uid);it.setString(2,friendUid);it.executeUpdate()
        }
    }

    suspend fun acceptFriend(uid:String,friendUid:String)=db.query{c->
        c.autoCommit=false
        try{
            c.prepareStatement("UPDATE friendships SET status='accepted',updated_at=NOW() WHERE user_uid=? AND friend_uid=? AND status='pending'").use{
                it.setString(1,friendUid);it.setString(2,uid);require(it.executeUpdate()==1){"Friend request not found"}
            }
            c.prepareStatement("INSERT INTO friendships(user_uid,friend_uid,status) VALUES(?,?, 'accepted') ON CONFLICT(user_uid,friend_uid) DO UPDATE SET status='accepted',updated_at=NOW()").use{
                it.setString(1,uid);it.setString(2,friendUid);it.executeUpdate()
            }
            c.commit()
        }catch(t:Throwable){c.rollback();throw t}finally{c.autoCommit=true}
    }

    suspend fun recentMessages(roomType:String,roomId:String?,limit:Int=30):List<ChatMessageDto>=db.query{c->
        val sql=if(roomId==null)
            "SELECT m.id,m.room_type,m.room_id,m.body,m.created_at,u.uid,u.email,u.nickname,u.avatar_url,u.bio FROM chat_messages m JOIN users u ON u.uid=m.sender_uid WHERE m.room_type=? AND m.room_id IS NULL ORDER BY m.created_at DESC LIMIT ?"
        else
            "SELECT m.id,m.room_type,m.room_id,m.body,m.created_at,u.uid,u.email,u.nickname,u.avatar_url,u.bio FROM chat_messages m JOIN users u ON u.uid=m.sender_uid WHERE m.room_type=? AND m.room_id=? ORDER BY m.created_at DESC LIMIT ?"
        c.prepareStatement(sql).use{
            it.setString(1,roomType);var i=2;if(roomId!=null)it.setString(i++,roomId);it.setInt(i,limit)
            it.executeQuery().use{rs->buildList{while(rs.next())add(message(rs))}.asReversed()}
        }
    }

    suspend fun addMessage(senderUid:String,r:SendMessageRequest):ChatMessageDto=db.query{c->
        require(r.roomType in setOf("global","anime","watch")){"Invalid room type"}
        require(r.body.trim().length in 1..1000){"Message must be 1..1000 characters"}
        if(r.roomType!="global")require(!r.roomId.isNullOrBlank()){"roomId is required"}
        val id=UUID.randomUUID()
        c.prepareStatement("INSERT INTO chat_messages(id,room_type,room_id,sender_uid,body) VALUES(?,?,?,?,?)").use{
            it.setObject(1,id);it.setString(2,r.roomType);it.setString(3,r.roomId);it.setString(4,senderUid);it.setString(5,r.body.trim());it.executeUpdate()
        }
        ChatMessageDto(id.toString(),r.roomType,r.roomId,c.user(senderUid)!!,r.body.trim(),OffsetDateTime.now().toString())
    }

    suspend fun leaderboard(limit:Int=20):List<LeaderboardEntryDto>=db.query{c->
        c.prepareStatement("SELECT u.uid,u.nickname,u.avatar_url,COALESCE(p.points,0) FROM users u LEFT JOIN social_points p ON p.uid=u.uid ORDER BY COALESCE(p.points,0) DESC,u.nickname LIMIT ?").use{
            it.setInt(1,limit);it.executeQuery().use{rs->buildList{while(rs.next())add(LeaderboardEntryDto(rs.getString(1),rs.getString(2),rs.getString(3),rs.getLong(4)))}}
        }
    }

    suspend fun activeFriends(uid:String):List<FriendDto>=db.query{c->
        c.prepareStatement("SELECT u.uid,u.nickname,u.avatar_url,'accepted' FROM friendships f JOIN users u ON u.uid=f.friend_uid WHERE f.user_uid=? AND f.status='accepted' AND u.last_seen_at>NOW()-INTERVAL '10 minutes' ORDER BY u.last_seen_at DESC").use{
            it.setString(1,uid);it.executeQuery().use{rs->buildList{while(rs.next())add(FriendDto(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4)))}}
        }
    }

    suspend fun activity(uid:String,limit:Int=20):List<ActivityDto>=db.query{c->
        c.prepareStatement("SELECT a.id,a.activity_type,a.payload,a.created_at,u.uid,u.email,u.nickname,u.avatar_url,u.bio FROM friend_activity a JOIN users u ON u.uid=a.actor_uid WHERE a.actor_uid IN (SELECT friend_uid FROM friendships WHERE user_uid=? AND status='accepted') OR a.actor_uid=? ORDER BY a.created_at DESC LIMIT ?").use{
            it.setString(1,uid);it.setString(2,uid);it.setInt(3,limit)
            it.executeQuery().use{rs->buildList{
                while(rs.next())add(ActivityDto(rs.getString(1),UserDto(rs.getString(5),rs.getString(6),rs.getString(7),rs.getString(8),rs.getString(9)),rs.getString(2),emptyMap(),rs.getObject(4,OffsetDateTime::class.java).toString()))
            }}
        }
    }

    suspend fun createWatchRoom(uid:String,r:CreateWatchRoomRequest):WatchRoomDto=db.query{c->
        val id=UUID.randomUUID();c.autoCommit=false
        try{
            c.prepareStatement("INSERT INTO watch_rooms(id,owner_uid,anime_id,episode_number,position_ms) VALUES(?,?,?,?,?)").use{
                it.setObject(1,id);it.setString(2,uid);it.setString(3,r.animeId);it.setInt(4,r.episodeNumber);it.setLong(5,r.positionMs);it.executeUpdate()
            }
            c.prepareStatement("INSERT INTO watch_room_members(room_id,uid) VALUES(?,?)").use{it.setObject(1,id);it.setString(2,uid);it.executeUpdate()}
            c.commit();WatchRoomDto(id.toString(),uid,r.animeId,r.episodeNumber,"paused",r.positionMs)
        }catch(t:Throwable){c.rollback();throw t}finally{c.autoCommit=true}
    }

    suspend fun updateWatchRoom(uid:String,id:String,r:UpdateWatchRoomRequest):WatchRoomDto=db.query{c->
        require(r.state in setOf("playing","paused","ended")){"Invalid watch state"}
        c.prepareStatement("UPDATE watch_rooms SET state=?,position_ms=?,updated_at=NOW() WHERE id=? AND owner_uid=?").use{
            it.setString(1,r.state);it.setLong(2,r.positionMs);it.setObject(3,UUID.fromString(id));it.setString(4,uid);require(it.executeUpdate()==1){"Watch room not found or not owned"}
        }
        c.prepareStatement("SELECT owner_uid,anime_id,episode_number,state,position_ms FROM watch_rooms WHERE id=?").use{
            it.setObject(1,UUID.fromString(id));it.executeQuery().use{rs->rs.next();WatchRoomDto(id,rs.getString(1),rs.getString(2),rs.getInt(3),rs.getString(4),rs.getLong(5))}
        }
    }

    suspend fun joinWatchRoom(uid:String,id:String)=db.query{c->
        c.prepareStatement("INSERT INTO watch_room_members(room_id,uid) VALUES(?,?) ON CONFLICT DO NOTHING").use{
            it.setObject(1,UUID.fromString(id));it.setString(2,uid);it.executeUpdate()
        }
    }

    private fun Connection.user(uid:String):UserDto?=prepareStatement("SELECT uid,email,nickname,avatar_url,bio FROM users WHERE uid=?").use{ps->
        ps.setString(1,uid);ps.executeQuery().use{rs->if(rs.next())UserDto(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5))else null}
    }

    private fun message(rs:ResultSet)=ChatMessageDto(
        rs.getObject(1).toString(),rs.getString(2),rs.getString(3),
        UserDto(rs.getString(6),rs.getString(7),rs.getString(8),rs.getString(9),rs.getString(10)),
        rs.getString(4),rs.getObject(5,OffsetDateTime::class.java).toString()
    )
}
