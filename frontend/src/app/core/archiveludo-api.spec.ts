import { TestBed } from '@angular/core/testing';
import { ArchiveludoApi } from './archiveludo-api';

describe('ArchiveludoApi', () => {
  let service: ArchiveludoApi;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ArchiveludoApi);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
