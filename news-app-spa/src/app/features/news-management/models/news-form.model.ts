// NewsCategory interface for use in news form
export interface NewsCategory {
  id: string;
  categoryNameEn: string;
  categoryNameEs: string;
  slug?: string;
  description?: string;
}
import { FormGroup, FormControl, Validators } from '@angular/forms';

export interface NewsFormModel {
  // Mandatory fields
  newsTitleEn: FormControl<string>;
  newsTitleEs: FormControl<string>;
  newsContentEn: FormControl<string>;
  newsContentEs: FormControl<string>;
  imageVideoFile: FormControl<File | null>;
  newsNewsCategoryId: FormControl<string>;
  newsSourceUrl: FormControl<string>;
  newsWorkflowStatus: FormControl<string>;
  newsScheduledPublishAt: FormControl<string>;
  newsIsFeatured: FormControl<boolean>;
  newsSlug: FormControl<string>;

  // Advanced fields
  newsExcerptEn: FormControl<string>;
  newsExcerptEs: FormControl<string>;
  newsContentFormat: FormControl<string>;
  newsTags: FormControl<string>;
  thumbnailFile: FormControl<File | null>;
  newsSourceAuthorName: FormControl<string>;
  newsSourceAgencyId: FormControl<string>;
  newsContentOrigin: FormControl<string>;
  newsIsBreaking: FormControl<boolean>;
  newsBreakingExpiresAt: FormControl<string>;
  newsEmbargoUntil: FormControl<string>;
  newsExpiresAt: FormControl<string>;
  newsUrgencyLevel: FormControl<string>;
  newsTargetAudience: FormControl<string>;
  newsReadTimeMinutes: FormControl<number | null>;
  newsCountryCode: FormControl<string>;
  newsRegion: FormControl<string>;
  newsCity: FormControl<string>;
  newsLatitude: FormControl<number | null>;
  newsLongitude: FormControl<number | null>;
  newsIsSponsored: FormControl<boolean>;
  newsSponsorName: FormControl<string>;
  newsSponsorLogoUrl: FormControl<string>;
  newsSponsorWebsiteUrl: FormControl<string>;
  newsIsPremium: FormControl<boolean>;
  newsPremiumTier: FormControl<string>;
  newsMetaTitle: FormControl<string>;
  newsMetaDescription: FormControl<string>;
  newsKeywords: FormControl<string>;
  newsCanonicalUrl: FormControl<string>;
  newsSeriesId: FormControl<string>;
  newsSeriesOrder: FormControl<number | null>;
  newsEditorNotes: FormControl<string>;
}

export function createNewsForm(): FormGroup<NewsFormModel> {
  return new FormGroup<NewsFormModel>({
    // Mandatory fields
    newsTitleEn: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    newsTitleEs: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    newsContentEn: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    newsContentEs: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    imageVideoFile: new FormControl(null, { nonNullable: true, validators: [Validators.required] }),
    newsNewsCategoryId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    newsSourceUrl: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.pattern('https?://.+')] }),
    newsWorkflowStatus: new FormControl('DRAFT', { nonNullable: true, validators: [Validators.required] }),
    newsScheduledPublishAt: new FormControl('', { nonNullable: true }),
    newsIsFeatured: new FormControl(false, { nonNullable: true }),
    newsSlug: new FormControl('', { nonNullable: true }),
    // Advanced fields
    newsExcerptEn: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(500)] }),
    newsExcerptEs: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(500)] }),
    newsContentFormat: new FormControl('PLAIN_TEXT', { nonNullable: true }),
    newsTags: new FormControl('', { nonNullable: true }),
    thumbnailFile: new FormControl(null, { nonNullable: true }),
    newsSourceAuthorName: new FormControl('', { nonNullable: true }),
    newsSourceAgencyId: new FormControl('', { nonNullable: true }),
    newsContentOrigin: new FormControl('ORIGINAL', { nonNullable: true }),
    newsIsBreaking: new FormControl(false, { nonNullable: true }),
    newsBreakingExpiresAt: new FormControl('', { nonNullable: true }),
    newsEmbargoUntil: new FormControl('', { nonNullable: true }),
    newsExpiresAt: new FormControl('', { nonNullable: true }),
    newsUrgencyLevel: new FormControl('NORMAL', { nonNullable: true }),
    newsTargetAudience: new FormControl('', { nonNullable: true }),
    newsReadTimeMinutes: new FormControl(null, { nonNullable: true }),
    newsCountryCode: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(2)] }),
    newsRegion: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(100)] }),
    newsCity: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(100)] }),
    newsLatitude: new FormControl(null, { nonNullable: true }),
    newsLongitude: new FormControl(null, { nonNullable: true }),
    newsIsSponsored: new FormControl(false, { nonNullable: true }),
    newsSponsorName: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(255)] }),
    newsSponsorLogoUrl: new FormControl('', { nonNullable: true }),
    newsSponsorWebsiteUrl: new FormControl('', { nonNullable: true }),
    newsIsPremium: new FormControl(false, { nonNullable: true }),
    newsPremiumTier: new FormControl('FREE', { nonNullable: true }),
    newsMetaTitle: new FormControl('', { nonNullable: true }),
    newsMetaDescription: new FormControl('', { nonNullable: true }),
    newsKeywords: new FormControl('', { nonNullable: true }),
    newsCanonicalUrl: new FormControl('', { nonNullable: true }),
    newsSeriesId: new FormControl('', { nonNullable: true }),
    newsSeriesOrder: new FormControl(null, { nonNullable: true }),
    newsEditorNotes: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(2000)] }),
  });
}
