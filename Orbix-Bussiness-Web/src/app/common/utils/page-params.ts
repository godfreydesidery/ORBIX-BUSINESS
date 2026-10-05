// Query parameters for one page of a list that is paged and searched on the server.
// Screens count pages from 1 and the server from 0; the server matches the search text against the shown columns.
export function pageParams(page: number, pageSize: number, search: string): string {
  return 'page=' + (page - 1) + '&size=' + pageSize + '&search=' + encodeURIComponent((search ?? '').trim())
}
