import { HttpContextToken, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { finalize } from 'rxjs';
import { LoaderService } from './loader.service';

// Set on a request to keep the loader hidden for it:
// this.http.get(url, { ...options, context: new HttpContext().set(SKIP_LOADER, true) })
export const SKIP_LOADER = new HttpContextToken<boolean>(() => false)

// Requests sent while the user types, where the overlay would get in the way:
// type-ahead lookups (product_name_like=, supplier_name_like=, ...) and paged-table filter boxes (a non-empty search=)
function sentWhileTyping(url: string): boolean {
  return url.includes('_like=') || /[?&]search=[^&]/.test(url)
}

// Shows the full-screen loader while any request is waiting for the server
export const loaderInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.context.get(SKIP_LOADER) || sentWhileTyping(req.url)) {
    return next(req)
  }
  const loader = inject(LoaderService)
  loader.start()
  return next(req).pipe(finalize(() => loader.stop()))
}
