// The server field takes the address only; the app adds /orbix-business-api.
//
// A pasted full API URL would otherwise double the path and every call would
// 404 — the same trap OrbixHQ hit with /api/v1.
import 'package:flutter_test/flutter_test.dart';

import 'package:orbix_manager/core/config/server_config.dart';

void main() {
  test('strips trailing slashes and a pasted API path', () {
    expect(ServerConfig.normalise(' http://13.51.26.114:8080/ '),
        'http://13.51.26.114:8080');
    expect(ServerConfig.normalise('http://13.51.26.114:8080/orbix-business-api'),
        'http://13.51.26.114:8080');
    expect(
        ServerConfig.normalise('http://13.51.26.114:8080/orbix-business-api/'),
        'http://13.51.26.114:8080');
    expect(ServerConfig.normalise('HTTP://HOST/ORBIX-BUSINESS-API'),
        'HTTP://HOST');
  });

  test('the API base is the address plus the path, or empty with no address',
      () {
    expect(ServerConfig.apiBaseFor('http://13.51.26.114:8080/'),
        'http://13.51.26.114:8080/orbix-business-api');
    expect(ServerConfig.apiBaseFor(''), '');
    expect(ServerConfig(host: '  ').hasHost, isFalse);
  });

  test('accepts an address, refuses anything else', () {
    expect(ServerConfig.looksValid('http://13.51.26.114:8080'), isTrue);
    expect(ServerConfig.looksValid('https://orbix.example.com'), isTrue);
    expect(
        ServerConfig.looksValid('http://13.51.26.114:8080/orbix-business-api'),
        isTrue);
    expect(ServerConfig.looksValid('13.51.26.114:8080'), isFalse);
    expect(ServerConfig.looksValid('http://host/other/path'), isFalse);
    expect(ServerConfig.looksValid(''), isFalse);
  });
}
