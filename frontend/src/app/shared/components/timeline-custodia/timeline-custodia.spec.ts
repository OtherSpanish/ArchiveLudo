import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TimelineCustodia } from './timeline-custodia';

describe('TimelineCustodia', () => {
  let component: TimelineCustodia;
  let fixture: ComponentFixture<TimelineCustodia>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TimelineCustodia],
    }).compileComponents();

    fixture = TestBed.createComponent(TimelineCustodia);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
