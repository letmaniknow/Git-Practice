import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';

import { NewsRoutingModule } from './news-routing.module';
import { newsSchedulerReducer } from './store/news-scheduler.reducer';
import { NewsSchedulerEffects } from './store/news-scheduler.effects';
import { MatTooltipModule } from '@angular/material/tooltip';

@NgModule({
  imports: [
    CommonModule,
    RouterModule,
    NewsRoutingModule,
    StoreModule.forFeature('newsScheduler', newsSchedulerReducer),
    EffectsModule.forFeature([NewsSchedulerEffects]),
    MatTooltipModule,
  ],
  declarations: [],
  exports: [],
})
export class NewsModule {}
