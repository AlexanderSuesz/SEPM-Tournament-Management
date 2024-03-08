import {formatDate} from "@angular/common";

export function formatIsoDate(date: Date): string {
  return formatDate(date, 'YYYY-MM-dd', 'en-DK');
}
