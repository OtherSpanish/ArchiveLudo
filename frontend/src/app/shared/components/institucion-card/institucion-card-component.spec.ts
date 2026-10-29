import { ComponentFixture, TestBed } from '@angular/core/testing';
import { InstitucionCardComponent } from './institucion-card-component';

describe('InstitucionCard', () => {
  let component: InstitucionCardComponent;
  let fixture: ComponentFixture<InstitucionCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [InstitucionCardComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(InstitucionCardComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
