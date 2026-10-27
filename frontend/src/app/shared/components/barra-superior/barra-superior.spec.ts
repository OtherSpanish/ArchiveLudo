import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BarraSuperior } from './barra-superior';

describe('BarraSuperior', () => {
  let component: BarraSuperior;
  let fixture: ComponentFixture<BarraSuperior>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [BarraSuperior],
    }).compileComponents();

    fixture = TestBed.createComponent(BarraSuperior);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
