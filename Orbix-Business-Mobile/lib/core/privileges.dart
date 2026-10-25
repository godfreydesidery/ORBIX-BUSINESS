/// Privilege codes the app gates on — the same codes the web app's menu
/// checks (`menu_items.ts`), read from the token's `privileges` claim.
///
/// The server does not enforce most of these itself, so the gate here is what
/// keeps a cashier's login from showing management figures.
class Privileges {
  Privileges._();

  /// The web app's Management menu: Management Board, Cash Collections.
  static const management = 'MNGNT-ACCESS';

  /// Finance › Reports.
  static const financeReports = 'FINCRPRT-ACCESS';

  static const admin = 'ADMIN-ACCESS';

  /// Who sees the dashboard: whoever the web app shows the Management Board
  /// or the finance reports to.
  static const dashboard = [management, financeReports, admin];

  /// Finance › Parking Discounts.
  static const parkingDiscounts = ['FINCDISC-ACCESS', 'PKNGDISC-ACCESS'];

  /// Finance › Storage Discounts.
  static const storageDiscounts = ['FINCDISC-ACCESS', 'STRGDISC-ACCESS'];

  /// Finance › Bond Discounts.
  static const bondDiscounts = ['BNDDISC-ACCESS'];

  /// The Procurement menu: LPOs and GRNs.
  static const procurement = 'PRCMT-ACCESS';
}
