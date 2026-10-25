import 'dart:convert';
import 'dart:typed_data';

import 'package:dio/dio.dart';

/// One request the app made.
class SeenRequest {
  SeenRequest(this.method, this.path, this.query, this.body);

  final String method;
  final String path;
  final Map<String, dynamic> query;
  final dynamic body;
}

/// A reply: JSON (or text) and a status.
class Reply {
  const Reply(this.body, {this.status = 200});

  final Object? body;
  final int status;
}

/// Stands in for the Orbix server. Routes by path (without the
/// `/orbix-business-api` prefix); the handler sees the request so it can
/// answer differently per date range.
class FakeServer implements HttpClientAdapter {
  FakeServer(this.routes);

  final Map<String, Reply Function(SeenRequest)> routes;
  final List<SeenRequest> seen = [];

  @override
  Future<ResponseBody> fetch(
    RequestOptions options,
    Stream<Uint8List>? requestStream,
    Future<void>? cancelFuture,
  ) async {
    final path = options.path.replaceFirst('/orbix-business-api', '');
    final req = SeenRequest(
      options.method,
      path,
      options.queryParameters,
      options.data,
    );
    seen.add(req);
    final handler = routes[path];
    final reply = handler == null
        ? const Reply('Not found', status: 404)
        : handler(req);
    final body = reply.body;
    return ResponseBody.fromString(
      body is String ? body : jsonEncode(body),
      reply.status,
      headers: {
        Headers.contentTypeHeader: [
          body is String ? 'text/plain' : Headers.jsonContentType,
        ],
      },
    );
  }

  @override
  void close({bool force = false}) {}
}
