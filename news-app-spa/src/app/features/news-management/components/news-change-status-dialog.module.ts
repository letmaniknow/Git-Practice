import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { NewsChangeStatusDialogComponent } from './news-change-status-dialog.component';

@NgModule({
  declarations: [NewsChangeStatusDialogComponent],
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSelectModule,
    MatButtonModule
  ],
  exports: [NewsChangeStatusDialogComponent],
})
export class NewsChangeStatusDialogModule {}
