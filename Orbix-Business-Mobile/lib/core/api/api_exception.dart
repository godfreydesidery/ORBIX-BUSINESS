import 'package:dio/dio.dart';

/// The single error type the app branches on.
///
/// The Orbix Business API has no response envelope. A refusal arrives as:
///
/// * plain text from `ExceptionController` — `"Invalid operation: ..."` with
///   409, `"Missing information: ..."` with 417, and so on;
/// * `{"error_message": ...}` with 403 from `CustomAuthorizationFilter` when the
///   token is expired or invalid — the session is over;
/// * Spring's default `{"status", "error", "message"}` (401 on a failed login,
///   403 when `@PreAuthorize` refuses).
///
/// UI branches on [statusCode] and [isSessionLost], never on the text, which is
/// only shown to the manager.
class ApiException implements Exception {
  ApiException({
    required this.statusCode,
    required this.message,
    this.isNetwork = false,
    this.isTimeout = false,
    this.isSessionLost = false,
    this.cause,
  });

  /// HTTP status, or null when no response was received (network/timeout).
  final int? statusCode;

  /// Friendly message for the manager.
  final String message;

  final bool isNetwork;
  final bool isTimeout;

  /// The server rejected the token itself: sign in again. Distinct from a
  /// plain 403, which refuses one action but leaves the session alone.
  final bool isSessionLost;

  final Object? cause;

  bool get isUnauthorized => statusCode == 401;
  bool get isForbidden => statusCode == 403 && !isSessionLost;
  bool get isNotFound => statusCode == 404;
  bool get isConflict => statusCode == 409;
  bool get isServer => statusCode != null && statusCode! >= 500;

  factory ApiException.fromDio(DioException e) {
    switch (e.type) {
      case DioExceptionType.connectionTimeout:
      case DioExceptionType.sendTimeout:
      case DioExceptionType.receiveTimeout:
      case DioExceptionType.transformTimeout:
        return ApiException(
          statusCode: null,
          isTimeout: true,
          message: 'The server did not respond in time. Try again.',
          cause: e,
        );
      case DioExceptionType.connectionError:
      case DioExceptionType.unknown:
        return ApiException(
          statusCode: null,
          isNetwork: true,
          message: 'Cannot reach the server. Check your connection.',
          cause: e,
        );
      case DioExceptionType.badCertificate:
        return ApiException(
          statusCode: null,
          isNetwork: true,
          message: 'The server certificate was rejected.',
          cause: e,
        );
      case DioExceptionType.cancel:
        return ApiException(
          statusCode: null,
          message: 'The request was cancelled.',
          cause: e,
        );
      case DioExceptionType.badResponse:
        return ApiException.fromResponse(e.response!, cause: e);
    }
  }

  factory ApiException.fromResponse(Response response, {Object? cause}) {
    final status = response.statusCode;
    final data = response.data;
    final sessionLost = status == 401 ||
        (status == 403 && data is Map && data.containsKey('error_message'));
    return ApiException(
      statusCode: status,
      isSessionLost: sessionLost,
      message: _messageFor(status, _serverText(data), sessionLost),
      cause: cause,
    );
  }

  /// The server's own words, when it sent a sentence a person can read.
  static String? _serverText(dynamic data) {
    if (data is String) {
      final s = data.trim();
      // An HTML error page is not a message.
      if (s.isEmpty || s.startsWith('<')) return null;
      return s.length > 300 ? '${s.substring(0, 300)}…' : s;
    }
    return null;
  }

  static String _messageFor(int? status, String? server, bool sessionLost) {
    if (sessionLost) return 'Your session has ended. Please sign in again.';
    switch (status) {
      case 403:
        return 'You do not have permission for this action.';
      case 404:
        return server ?? 'Not found.';
      default:
        if (server != null) return server;
        if (status != null && status >= 500) {
          return 'The server had an error. Please try again.';
        }
        return 'Something went wrong (HTTP $status).';
    }
  }

  @override
  String toString() => 'ApiException($statusCode): $message';
}
