import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FichaVideojuegoComponent } from './ficha-videojuego-component';

describe('FichaVideojuego', () => {
  let component: FichaVideojuegoComponent;
  let fixture: ComponentFixture<FichaVideojuegoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [FichaVideojuegoComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(FichaVideojuegoComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
