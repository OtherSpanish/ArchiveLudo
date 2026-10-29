import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProyectoCardComponent } from './proyecto-card-component';

describe('ProyectoCardComponent', () => {
  let component: ProyectoCardComponent;
  let fixture: ComponentFixture<ProyectoCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ProyectoCardComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ProyectoCardComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
