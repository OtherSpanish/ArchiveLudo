import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FichaInstitucionComponent } from './ficha-institucion-component';

describe('FichaInstitucion', () => {
  let component: FichaInstitucionComponent;
  let fixture: ComponentFixture<FichaInstitucionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [FichaInstitucionComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(FichaInstitucionComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
