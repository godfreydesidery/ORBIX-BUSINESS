import { Component, ViewEncapsulation } from '@angular/core';
import { ActivatedRoute, Router, ActivatedRouteSnapshot, UrlSegment, NavigationEnd, RouterLink } from "@angular/router";
import { Title } from '@angular/platform-browser';
import { SettingsService } from '@services/settings.service';

@Component({
    selector: 'az-breadcrumb',
    standalone: true,
    imports: [
        RouterLink
    ],
    encapsulation: ViewEncapsulation.None,
    styleUrls: ['./breadcrumb.component.scss'],
    templateUrl: './breadcrumb.component.html'
})
export class BreadcrumbComponent {
    public settings: any;
    public title: string;
    public breadcrumbs: {
        name: string;
        url: string
    }[] = [];

    constructor(public _router: Router, private _settingsService: SettingsService, private _title: Title) {
        this.settings = this._settingsService.settings;
        this._router.events.subscribe(event => {
            if (event instanceof NavigationEnd) {
                this.breadcrumbs = [];
                this.parseRoute(this._router.routerState.snapshot.root);
                this.title = "Davaghan";
                this.breadcrumbs.forEach(breadcrumb => {
                    this.title += ' > ' + breadcrumb.name;
                })
                //this._title.setTitle(this.settings.name + this.title);
                this._title.setTitle(this.title)
            }
        })
    }

    parseRoute(node: ActivatedRouteSnapshot) {
        if (node.data['breadcrumb']) {
            if (node.url.length) {
                let urlSegments: UrlSegment[] = [];
                node.pathFromRoot.forEach(routerState => {
                    urlSegments = urlSegments.concat(routerState.url);
                });
                let url = urlSegments.map(urlSegment => {
                    return urlSegment.path;
                }).join('/');
                this.breadcrumbs.push({
                    name: node.data['breadcrumb'],
                    url: '/' + url
                })
            }
        }
        if (node.firstChild) {
            this.parseRoute(node.firstChild);
        }
    }
} 