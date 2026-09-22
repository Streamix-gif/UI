package ani.saikou.backend

sealed interface BackendResult<out T> {
    data class Success<T>(val value: T) : BackendResult<T>

    data class HttpError(
        val status: Int,
        val message: String? = null
    ) : BackendResult<Nothing>

    data class NetworkError(
        val cause: Throwable
    ) : BackendResult<Nothing>

    data object NotConfigured : BackendResult<Nothing>
}
