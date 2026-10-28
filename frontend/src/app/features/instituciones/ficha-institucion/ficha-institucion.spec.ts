import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FichaInstitucion } from './ficha-institucion';

describe('FichaInstitucion', () => {
  let component: FichaInstitucion;
  let fixture: ComponentFixture<FichaInstitucion>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [FichaInstitucion],
    }).compileComponents();

    fixture = TestBed.createComponent(FichaInstitucion);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
