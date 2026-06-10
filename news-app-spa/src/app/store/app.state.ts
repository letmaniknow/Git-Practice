/**
 * Root application state interface
 * Defines the shape of the entire Redux store
 */

import { NewsState } from './news/news.state';

export interface AppState {
  news: NewsState;
}
