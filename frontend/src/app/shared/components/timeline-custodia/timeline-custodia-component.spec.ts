import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TimelineCustodiaComponent } from './timeline-custodia-component';

describe('TimelineCustodia', () => {
  let component: TimelineCustodiaComponent;
  let fixture: ComponentFixture<TimelineCustodiaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TimelineCustodiaComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(TimelineCustodiaComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
