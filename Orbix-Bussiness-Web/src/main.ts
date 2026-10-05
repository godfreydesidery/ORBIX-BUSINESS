import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';
import { environment } from './environments/environment';


//for ng2-dragula
(window as any).global = window;

// Production builds do not print console.log output: the screens log whole API responses,
// which the browser keeps in memory while DevTools is open. Warnings and errors still show.
if (environment.production) {
  console.log = () => {};
}

bootstrapApplication(AppComponent, appConfig)
  .catch((err) => console.error(err));