import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EstadoBadgeComponent } from './estado-badge-component';

describe('EstadoBadge', () => {
  let component: EstadoBadgeComponent;
  let fixture: ComponentFixture<EstadoBadgeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [EstadoBadgeComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(EstadoBadgeComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
