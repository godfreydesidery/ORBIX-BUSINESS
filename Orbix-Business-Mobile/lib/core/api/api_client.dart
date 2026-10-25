import 'package:dio/dio.dart';

import 'api_exception.dart';

/// The authenticated client for ONE account on one Orbix server.
///
/// Sends the account's bearer token and maps every failure to [ApiException].
/// There is no transparent refresh: the server's `/token/refresh` issues an
/// access token the authorization filter cannot read (its privileges sit under
/// `roles`, the filter reads `privileges`), so a refreshed session would fail
/// on its first call. When the token is rejected the session is over and
/// [onSessionLost] routes the manager back to sign-in — the same thing the web
/// app ends up doing.
class ApiClient {
  ApiClient({
    required String apiBase,
    required String accessToken,
    HttpClientAdapter? adapter,
  }) : _dio = Dio(BaseOptions(
          baseUrl: apiBase,
          contentType: Headers.jsonContentType,
          connectTimeout: const Duration(seconds: 15),
          receiveTimeout: const Duration(seconds: 45),
          headers: {'Authorization': 'Bearer $accessToken'},
        )) {
    if (adapter != null) _dio.httpClientAdapter = adapter;
  }

  final Dio _dio;

  /// Invoked once when the server rejects the token.
  void Function()? onSessionLost;

  Future<dynamic> get(String path, {Map<String, dynamic>? query}) =>
      _send(() => _dio.get(path, queryParameters: _clean(query)));

  Future<dynamic> post(
    String path, {
    Object? body,
    Map<String, dynamic>? query,
  }) =>
      _send(() => _dio.post(path, data: body, queryParameters: _clean(query)));

  Future<dynamic> _send(Future<Response> Function() call) async {
    try {
      final res = await call();
      return res.data;
    } on DioException catch (e) {
      final error = ApiException.fromDio(e);
      if (error.isSessionLost) onSessionLost?.call();
      throw error;
    }
  }

  Map<String, dynamic>? _clean(Map<String, dynamic>? q) {
    if (q == null) return null;
    return {
      for (final e in q.entries)
        if (e.value != null) e.key: e.value,
    };
  }

  /// Signs in and returns the access token. Unauthenticated, form-encoded —
  /// `CustomAuthenticationFilter` reads `username` and `password` as request
  /// parameters, not JSON.
  static Future<String> login({
    required String apiBase,
    required String username,
    required String password,
  }) async {
    final dio = Dio(BaseOptions(
      baseUrl: apiBase,
      connectTimeout: const Duration(seconds: 15),
      receiveTimeout: const Duration(seconds: 30),
    ));
    try {
      final res = await dio.post(
        '/login',
        data: {'username': username, 'password': password},
        options: Options(contentType: Headers.formUrlEncodedContentType),
      );
      final data = res.data;
      final token = data is Map ? data['access_token']?.toString() : null;
      if (token == null || token.isEmpty) {
        throw ApiException(
          statusCode: res.statusCode,
          message: 'The server did not return a session. Is this an '
              'Orbix Business server?',
        );
      }
      return token;
    } on DioException catch (e) {
      throw ApiException.fromDio(e);
    }
  }
}
