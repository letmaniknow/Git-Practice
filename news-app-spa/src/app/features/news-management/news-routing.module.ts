import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { NewsSchedulerManagementPageComponent } from './pages/news-scheduler-management-page/news-scheduler-management-page.component';

const routes: Routes = [
  {
    path: 'scheduler',
    component: NewsSchedulerManagementPageComponent,
    data: { title: 'News Scheduler' },
  },
  // Define additional news feature routes here
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class NewsRoutingModule {}
