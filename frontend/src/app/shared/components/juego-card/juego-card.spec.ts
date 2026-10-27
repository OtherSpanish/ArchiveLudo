import { ComponentFixture, TestBed } from '@angular/core/testing';
import { JuegoCard } from './juego-card';

describe('JuegoCard', () => {
  let component: JuegoCard;
  let fixture: ComponentFixture<JuegoCard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [JuegoCard],
    }).compileComponents();

    fixture = TestBed.createComponent(JuegoCard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
