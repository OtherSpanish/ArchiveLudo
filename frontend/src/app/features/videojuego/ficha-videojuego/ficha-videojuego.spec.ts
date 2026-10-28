import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FichaVideojuego } from './ficha-videojuego';

describe('FichaVideojuego', () => {
  let component: FichaVideojuego;
  let fixture: ComponentFixture<FichaVideojuego>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [FichaVideojuego],
    }).compileComponents();

    fixture = TestBed.createComponent(FichaVideojuego);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
