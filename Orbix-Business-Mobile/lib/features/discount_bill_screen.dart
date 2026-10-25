import 'package:flutter/material.dart';

import '../app/app_scope.dart';
import '../app/format.dart';
import '../app/theme.dart';
import '../models/approvals.dart';
import '../services/approvals_service.dart';
import '../widgets/async_view.dart';
import '../widgets/common.dart';
import '../widgets/kit.dart';
import 'approvals_screen.dart';

/// The bill behind a discount request: the item, every bill raised on it —
/// the one under request marked — and what is paid and owing. The decision
/// can be made here too; the screen then pops `true` so the queue reloads.
class DiscountBillScreen extends StatefulWidget {
  const DiscountBillScreen({super.key, required this.discount});

  final PendingDiscount discount;

  @override
  State<DiscountBillScreen> createState() => _DiscountBillScreenState();
}

typedef _BillData = ({List<BillLine> bills, List<(String, String)> facts});

class _DiscountBillScreenState extends State<DiscountBillScreen> {
  bool _busy = false;

  Future<void> _decide({required bool approve}) async {
    final decided = await decideDiscount(
      context,
      widget.discount,
      approve: approve,
      onStart: () => setState(() => _busy = true),
      onFailed: () {
        if (mounted) setState(() => _busy = false);
      },
    );
    if (decided == true && mounted) Navigator.of(context).pop(true);
  }

  @override
  Widget build(BuildContext context) {
    final d = widget.discount;
    final service = ApprovalsService(AppScope.of(context).session);
    return Scaffold(
      backgroundColor: HqColors.bg,
      appBar: AppBar(title: Text('Bill · ${d.item.no}')),
      body: AsyncView<_BillData>(
        load: () async {
          final results = await Future.wait<Object>([
            service.bills(d.item),
            // The facts only dress the screen; their absence must not stop
            // the bill from showing.
            service.itemFacts(d.item).catchError((_) => <(String, String)>[]),
          ]);
          return (
            bills: results[0] as List<BillLine>,
            facts: results[1] as List<(String, String)>,
          );
        },
        builder: (context, data) => _BillBody(discount: d, data: data),
      ),
      bottomNavigationBar: SafeArea(
        child: Padding(
          padding: const EdgeInsets.fromLTRB(20, 8, 20, 12),
          child: Row(
            children: [
              Expanded(
                child: OutlinedButton(
                  onPressed: _busy ? null : () => _decide(approve: false),
                  style: OutlinedButton.styleFrom(
                    foregroundColor: HqColors.bad,
                  ),
                  child: const Text('Reject'),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: FilledButton(
                  onPressed: _busy ? null : () => _decide(approve: true),
                  child: _busy
                      ? const SizedBox(
                          width: 20,
                          height: 20,
                          child: CircularProgressIndicator(
                            strokeWidth: 2.2,
                            color: Colors.white,
                          ),
                        )
                      : const Text('Approve'),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _BillBody extends StatelessWidget {
  const _BillBody({required this.discount, required this.data});

  final PendingDiscount discount;
  final _BillData data;

  @override
  Widget build(BuildContext context) {
    final d = discount;
    final item = d.item;
    final bills = data.bills;
    final requested = billFor(d, bills);
    final billed = bills.fold<double>(0, (a, b) => a + b.amount);
    final paid =
        bills.where((b) => b.paid).fold<double>(0, (a, b) => a + b.amount);
    final owing = billed - paid;

    return ListView(
      padding: const EdgeInsets.fromLTRB(20, 12, 20, 28),
      children: [
        HqCard(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(item.kind.label.toUpperCase(), style: HqText.section),
              const SizedBox(height: 6),
              if (item.title.isNotEmpty) Text(item.title, style: HqText.verdict),
              if (item.owner.isNotEmpty) Text(item.owner, style: HqText.body),
              if (item.detail.isNotEmpty) Text(item.detail, style: HqText.tiny),
              if (data.facts.isNotEmpty) ...[
                const Divider(height: 18),
                for (final (label, value) in data.facts)
                  FigureRow(label: label, value: value),
              ],
            ],
          ),
        ),
        const SizedBox(height: 22),
        const SectionLabel(text: 'The request'),
        HqCard(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              FigureRow(label: 'Bill', value: tzs(d.billAmount)),
              FigureRow(
                label: 'Discount asked',
                value: '− ${tzs(d.discountAmount)}',
                valueColor: HqColors.warn,
              ),
              FigureRow(
                label: 'To pay after discount',
                value: tzs(d.afterDiscount),
                emphasise: true,
              ),
              if (d.reason.isNotEmpty) ...[
                const SizedBox(height: 6),
                Text('Reason: ${d.reason}', style: HqText.body),
              ],
            ],
          ),
        ),
        const SizedBox(height: 22),
        SectionLabel(
          text: 'Bills on this ${item.kind.label.toLowerCase()}',
          trailing: '${bills.length} ${bills.length == 1 ? 'BILL' : 'BILLS'}',
        ),
        if (bills.isEmpty)
          const HqCard(
            child: Text('The server has no bills for this item.',
                style: HqText.body),
          )
        else ...[
          HqCard(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
            child: Column(
              children: [
                for (var i = 0; i < bills.length; i++) ...[
                  if (i > 0) const Divider(height: 1),
                  _BillRow(bill: bills[i], requested: bills[i] == requested),
                ],
              ],
            ),
          ),
          const SizedBox(height: 12),
          HqCard(
            child: Column(
              children: [
                FigureRow(label: 'Billed in all', value: tzs(billed)),
                FigureRow(
                  label: 'Paid',
                  value: tzs(paid),
                  valueColor: HqColors.good,
                ),
                FigureRow(
                  label: 'Still owing',
                  value: tzs(owing),
                  emphasise: true,
                  valueColor: owing > 0 ? HqColors.bad : null,
                ),
              ],
            ),
          ),
        ],
      ],
    );
  }
}

class _BillRow extends StatelessWidget {
  const _BillRow({required this.bill, required this.requested});

  final BillLine bill;

  /// The bill this discount request is for.
  final bool requested;

  @override
  Widget build(BuildContext context) {
    final b = bill;
    final span = [_day(b.from), _day(b.to)].where((s) => s.isNotEmpty).join(' – ');
    final days = b.days == b.days.roundToDouble()
        ? b.days.toStringAsFixed(0)
        : b.days.toStringAsFixed(1);
    return Container(
      margin: const EdgeInsets.symmetric(vertical: 6),
      padding: requested ? const EdgeInsets.all(10) : EdgeInsets.zero,
      decoration: requested
          ? BoxDecoration(
              color: HqColors.warnSoft,
              borderRadius: BorderRadius.circular(HqRadii.sm),
            )
          : null,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          if (requested)
            const Padding(
              padding: EdgeInsets.only(bottom: 4),
              child: Text(
                'DISCOUNT REQUESTED ON THIS BILL',
                style: TextStyle(
                  fontSize: 10.5,
                  fontWeight: FontWeight.w700,
                  letterSpacing: 0.6,
                  color: HqColors.warn,
                ),
              ),
            ),
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Expanded(
                child: Text(
                  span.isEmpty ? (b.description.isEmpty ? 'Bill' : b.description) : span,
                  style: const TextStyle(
                    fontSize: 14.5,
                    fontWeight: FontWeight.w600,
                    color: HqColors.ink,
                  ),
                ),
              ),
              const SizedBox(width: 12),
              Text(
                tzs(b.amount),
                style: const TextStyle(
                  fontSize: 14.5,
                  fontWeight: FontWeight.w700,
                  color: HqColors.ink,
                ),
              ),
            ],
          ),
          const SizedBox(height: 3),
          Row(
            children: [
              Expanded(
                child: Text(
                  [
                    if (b.days > 0) '$days ${b.days == 1 ? 'day' : 'days'} × ${tzs(b.price)}',
                    if (b.discount > 0) '${tzs(b.discount)} discount',
                  ].join(' · '),
                  style: HqText.tiny,
                ),
              ),
              _Status(paid: b.paid),
            ],
          ),
        ],
      ),
    );
  }

  static String _day(String s) {
    final d = DateTime.tryParse(s);
    return d == null ? s : formatDay(d);
  }
}

class _Status extends StatelessWidget {
  const _Status({required this.paid});

  final bool paid;

  @override
  Widget build(BuildContext context) {
    final color = paid ? HqColors.good : HqColors.bad;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
      decoration: BoxDecoration(
        color: paid ? HqColors.goodSoft : HqColors.badSoft,
        borderRadius: BorderRadius.circular(999),
      ),
      child: Text(
        paid ? 'PAID' : 'UNPAID',
        style: TextStyle(
          fontSize: 10.5,
          fontWeight: FontWeight.w700,
          letterSpacing: 0.6,
          color: color,
        ),
      ),
    );
  }
}
