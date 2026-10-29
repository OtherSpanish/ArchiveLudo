import { TestBed } from '@angular/core/testing';
import { ArchiveludoApiService } from './archiveludo-api-service';

describe('ArchiveludoApi', () => {
  let service: ArchiveludoApiService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ArchiveludoApiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
