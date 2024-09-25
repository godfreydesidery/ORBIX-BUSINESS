import { Component, ViewEncapsulation } from '@angular/core';
import { GoogleMapsModule } from '@angular/google-maps';

@Component({
  selector: 'az-google-maps',
  standalone: true,
  imports: [
    GoogleMapsModule
  ],
  templateUrl: './google-maps.component.html',
  styleUrl: './google-maps.component.scss',
  encapsulation: ViewEncapsulation.None
})
export class GoogleMapsComponent {
  center: google.maps.LatLngLiteral = { lat: 45.421530, lng: -75.697193 };
  zoom = 7;
  markerOptions: google.maps.MarkerOptions = { draggable: false };
  markerPositions: google.maps.LatLngLiteral[] = [
    { lat: 45.421530, lng: -75.697193 }
  ];
}
