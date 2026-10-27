import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EstadoBadge } from './estado-badge';

describe('EstadoBadge', () => {
  let component: EstadoBadge;
  let fixture: ComponentFixture<EstadoBadge>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [EstadoBadge],
    }).compileComponents();

    fixture = TestBed.createComponent(EstadoBadge);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
