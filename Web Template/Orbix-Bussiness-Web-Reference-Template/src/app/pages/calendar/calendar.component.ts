import { DatePipe } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SettingsService } from '@services/settings.service';
import { DirectivesModule } from 'src/app/theme/directives/directives.module';

@Component({
  selector: 'az-calendar',
  standalone: true,
  imports: [
    DirectivesModule,
    DatePipe,
    FormsModule
  ],
  templateUrl: './calendar.component.html' 
})
export class CalendarComponent {
  public settings: any; 

  calendarOptions: any;
  $calendar: any;
  dragOptions: Object = { zIndex: 999, revert: true, revertDuration: 0 };
  event: any = {};
  createEvent: any;

  constructor(private _settingsService: SettingsService) {
    this.settings = this._settingsService.settings; 

    let date = new Date();
    let d = date.getDate();
    let m = date.getMonth();
    let y = date.getFullYear();

    this.calendarOptions = {
      header: {
        left: 'today prev,next',
        center: 'title',
        right: 'month,agendaWeek,agendaDay,listMonth'
      },
      events: [
        {
          title: 'All Day Event',
          start: new Date(y, m, 1),
          backgroundColor: this.settings.colors.primary,
          textColor: this.settings.colors.default,
          description: 'Will be busy throughout the whole day'
        },
        {
          title: 'Long Event',
          start: new Date(y, m, d + 5),
          end: new Date(y, m, d + 7),
          description: 'This conference should be worse visiting'
        },
        {
          id: 999,
          title: 'Blah Blah Car',
          start: new Date(y, m, d - 3, 16, 0),
          allDay: false,
          description: 'Agree with this guy on arrival time'
        },
        {
          id: 1000,
          title: 'Buy this template',
          start: new Date(y, m, d + 3, 12, 0),
          allDay: false,
          backgroundColor: this.settings.colors.warning,
          textColor: this.settings.colors.default,
          description: 'Make sure everything is consistent first'
        },
        {
          title: 'Got to school',
          start: new Date(y, m, d + 16, 12, 0),
          end: new Date(y, m, d + 16, 13, 0),
          backgroundColor: this.settings.colors.danger,
          textColor: this.settings.colors.default,
          description: 'Time to go back'
        },
        {
          title: 'Study some Node',
          start: new Date(y, m, d + 18, 12, 0),
          end: new Date(y, m, d + 18, 13, 0),
          backgroundColor: this.settings.colors.success,
          textColor: this.settings.colors.default,
          description: 'Node.js is a platform built ' +
            'on Chrome\'s JavaScript runtime for easily' +
            ' building fast, scalable network applications.' +
            ' Node.js uses an event-driven, non-blocking' +
            ' I/O model that makes it lightweight and' +
            ' efficient, perfect for data-intensive real-time' +
            ' applications that run across distributed devices.'
        },
        {
          title: 'Azimuth link',
          start: new Date(y, m, 28),
          end: new Date(y, m, 29),
          url: 'http://themeseason.com/',
          backgroundColor: this.settings.colors.info,
          textColor: this.settings.colors.default,
          description: this.settings.title
        }
      ],
      eventColor: this.settings.colors.info,
      selectable: true,
      selectHelper: true,
      select: (start: any, end: any, allDay: any): void => {
        this.createEvent = () => {
          let title = this.event.title;
          if (title) {
            this.$calendar.fullCalendar('renderEvent',
              {
                title: title,
                start: start,
                end: end,
                allDay: allDay,
                backgroundColor: this.settings.colors.success,
                textColor: this.settings.colors.default
              },
              true // make the event "stick"
            );
          }
          this.$calendar.fullCalendar('unselect');
          jQuery('#create-event-modal').modal('hide');
        };

        jQuery('#create-event-modal').modal('show');
      },
      eventClick: (event: any): void => {
        this.event = event;
        jQuery('#show-event-modal').modal('show');
      },
      editable: true,
      droppable: true,

      drop: (dateItem: any, event: any): void => { // this function is called when something is dropped
        // retrieve the dropped element's stored Event Object
        let originalEventObject = {
          // use the element's text as the event title
          title: jQuery.trim(jQuery(event.target).text())
        };

        // we need to copy it, so that multiple events don't have a reference to the same object
        let copiedEventObject = jQuery.extend({}, originalEventObject);

        // assign it the date that was reported
        copiedEventObject.start = dateItem;
        copiedEventObject.allDay = !dateItem.hasTime();

        let $categoryClass = jQuery(event.target).data('event-class');
        if ($categoryClass) { copiedEventObject.className = [$categoryClass]; }

        // render the event on the calendar
        // the last `true` argument determines if
        // the event 'sticks'
        // http://arshaw.com/fullcalendar/docs/event_rendering/renderEvent/)
        this.$calendar.fullCalendar('renderEvent', copiedEventObject, true);

        jQuery(event.target).remove();

      },
      dayRender: function (date: any, cell: any) {
        let today = new Date().toDateString();
        let compareDate = date.toDate().toDateString();
        if (today == compareDate) {
          cell.css("background-color", "#ccc");
        }
      }
    };
  };

  addEvent(event: any): void {
    this.calendarOptions.events.push(event);
  };

  ngOnInit(): void {
    this.$calendar = jQuery('#calendar');
    this.$calendar.fullCalendar(this.calendarOptions);
    jQuery('.draggable').draggable(this.dragOptions);

  }

}
