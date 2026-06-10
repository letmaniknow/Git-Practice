import { NewsItem } from './news-item.model';

export interface PaginatedNewsResponse {
  status: string;
  message: string;
  timestamp: string;
  data: {
    content: NewsItem[];
    pageable: any; // You can further type this if needed
    totalElements: number;
    totalPages: number;
    last: boolean;
    size: number;
    number: number;
    sort: any;
    numberOfElements: number;
    first: boolean;
    empty: boolean;
  };
}
