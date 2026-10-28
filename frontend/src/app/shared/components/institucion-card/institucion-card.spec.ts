import { ComponentFixture, TestBed } from '@angular/core/testing';
import { InstitucionCard } from './institucion-card';

describe('InstitucionCard', () => {
  let component: InstitucionCard;
  let fixture: ComponentFixture<InstitucionCard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [InstitucionCard],
    }).compileComponents();

    fixture = TestBed.createComponent(InstitucionCard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
