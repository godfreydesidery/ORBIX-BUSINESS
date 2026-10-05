/**
 * trackBy for *ngFor: rows of an item with the same id are kept when a list is reloaded,
 * instead of being destroyed and rebuilt. Items without an id are tracked as before (by identity).
 */
export function trackById(index: number, item: any): any {
  return item?.id ?? item;
}
