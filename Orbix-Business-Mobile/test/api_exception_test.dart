// How the Orbix API's refusals are read.
//
// An expired token and a refused action are both 403, but only the first ends
// the session: the authorization filter answers with {"error_message": ...},
// @PreAuthorize with Spring's default body. Mixing them up would either sign a
// manager out for lacking one privilege, or leave them stuck on a dead token.
import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:orbix_manager/core/api/api_exception.dart';

ApiException _from(int status, dynamic body) => ApiException.fromResponse(
      Response(
        requestOptions: RequestOptions(path: '/x'),
        statusCode: status,
        data: body,
      ),
    );

void main() {
  test('a rejected token ends the session', () {
    final e = _from(403, {
      'error_message': 'The Token has expired on 2026-10-25T08:00:00Z.',
    });
    expect(e.isSessionLost, isTrue);
    expect(e.isForbidden, isFalse);
    expect(e.message, contains('sign in again'));
  });

  test('a refused action does not', () {
    final e = _from(403, {
      'status': 403,
      'error': 'Forbidden',
      'message': 'Access is denied',
    });
    expect(e.isSessionLost, isFalse);
    expect(e.isForbidden, isTrue);
  });

  test("the server's own sentence is shown", () {
    final e = _from(409, 'Invalid operation: LPO already approved');
    expect(e.message, 'Invalid operation: LPO already approved');
    expect(e.isSessionLost, isFalse);
  });

  test('an HTML error page is not shown as a message', () {
    final e = _from(500, '<html><body>Whitelabel Error Page</body></html>');
    expect(e.message, 'The server had an error. Please try again.');
  });
}
