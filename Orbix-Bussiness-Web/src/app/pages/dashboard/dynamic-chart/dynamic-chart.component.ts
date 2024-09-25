import { Component, ViewEncapsulation } from '@angular/core';
import { SettingsService } from '@services/settings.service';
import { ChartConfiguration, ChartOptions } from 'chart.js';
import { NgChartsModule } from 'ng2-charts';
import 'chart.js/dist/chart.js';

@Component({
  selector: 'az-dynamic-chart',
  standalone: true,
  imports: [
    NgChartsModule
  ],
  templateUrl: './dynamic-chart.component.html', 
  encapsulation: ViewEncapsulation.None
})
export class DynamicChartComponent {
  public settings: any; 

  public lineChartType: any = 'line';
  public lineChartData: ChartConfiguration<'line'>['data'] = {
    datasets: []
  };
  public lineChartOptions: ChartOptions<'line'>;

  public pieChartType: any = 'pie';
  public pieChartData: ChartConfiguration<'pie'>['data'] = {
    datasets: []
  };
  public pieChartOptions: ChartOptions<'pie'>;
  public pieChartLegend: boolean = true;

  constructor(private _settingsService: SettingsService) {
    this.settings = this._settingsService.settings; 
  }

  ngOnInit() {

    this.lineChartData.labels = ['January', 'February', 'March', 'April', 'May', 'June', 'July'];
    this.lineChartData.datasets = [
      {
        data: [11700, 10320, 25080, 32501, 24556, 49855, 21580],
        label: 'Web',
        fill: true,
        tension: 0.5,
        borderWidth: 2,
        backgroundColor: this._settingsService.rgba(this.settings.colors.danger, 0.5),
        borderColor: this.settings.colors.danger,
        pointBorderColor: this.settings.colors.default,
        pointHoverBorderColor: this.settings.colors.danger,
        pointHoverBackgroundColor: this.settings.colors.default,
        hoverBackgroundColor: this.settings.colors.danger
      },
      {
        data: [28080, 42750, 40548, 19256, 29566, 32589, 47500],
        label: 'Mobile',
        fill: true,
        tension: 0.5,
        borderWidth: 2,
        backgroundColor: this._settingsService.rgba(this.settings.colors.info, 0.5),
        borderColor: this.settings.colors.info,
        pointBorderColor: this.settings.colors.default,
        pointHoverBorderColor: this.settings.colors.info,
        pointHoverBackgroundColor: this.settings.colors.default,
        hoverBackgroundColor: this.settings.colors.info
      },
    ];
    this.lineChartOptions = {
      scales: {
        y: {
          display: true,
          beginAtZero: true,
          ticks: {
            color: this._settingsService.rgba(this.settings.colors.gray, 0.7),
            stepSize: 5000
          },
          grid: {
            display: true,
            color: this._settingsService.rgba(this.settings.colors.gray, 0.1)
          }
        },
        x: {
          display: true,
          beginAtZero: true,
          ticks: {
            color: this._settingsService.rgba(this.settings.colors.gray, 0.7)
          },
          grid: {
            display: true,
            color: this._settingsService.rgba(this.settings.colors.gray, 0.1)
          }
        }
      },
      plugins: {
        legend: {
          display: true,
          labels: {
            color: this._settingsService.rgba(this.settings.colors.gray, 0.9),
          }
        },
        tooltip: {
          enabled: true,
          backgroundColor: this._settingsService.rgba(this.settings.colors.main, 0.7)
        }
      }
    }


    this.pieChartData.labels = [['Profit'], ['Fees'], ['Tax']];
    this.pieChartData.datasets = [{
      data: [570, 150, 300],
      backgroundColor: [
        this._settingsService.rgba(this.settings.colors.success, 0.7),
        this._settingsService.rgba(this.settings.colors.warning, 0.7),
        this._settingsService.rgba(this.settings.colors.danger, 0.7)
      ],
      hoverBackgroundColor: [
        this.settings.colors.success,
        this.settings.colors.warning,
        this.settings.colors.danger
      ],
      borderColor: this.settings.colors.grayLight,
      borderWidth: 1,
      hoverBorderWidth: 3
    }];
    this.pieChartOptions = {
      plugins: {
        title: {
          display: true,
          text: 'Corporate Info With %',
          color: this.settings.colors.gray,
          font: {
            size: 14,
            style: 'normal',
            weight: 'normal'
          }
        },
        legend: {
          display: true,
          labels: {
            color: this._settingsService.rgba(this.settings.colors.gray, 0.9),
          }
        },
        tooltip: {
          enabled: true,
          backgroundColor: this._settingsService.rgba(this.settings.colors.main, 0.7),
          callbacks: {
            label: (context) => {
              let label = context.dataset.label || '';
              var total: any = context.dataset.data.reduce((previousValue: any, currentValue) => previousValue + currentValue);
              var currentValue: any = context.parsed;
              var precentage = Math.floor(((currentValue / total) * 100) + 0.5);
              if (label) {
                label += ': ';
              }
              label = context.label[0] + ': ' + precentage + '%';
              return label;
            }
          }
        }
      }
    }

  } 

  public randomizeType(): void {
    this.lineChartType = this.lineChartType === 'line' ? 'bar' : 'line';
    this.pieChartType = this.pieChartType === 'doughnut' ? 'pie' : 'doughnut';
  }

  public chartClicked(e: any): void {
    // console.log(e);
  }

  public chartHovered(e: any): void {
    // console.log(e);
  } 

} 