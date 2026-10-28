import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BannerPrioridad } from './banner-prioridad';

describe('BannerPrioridad', () => {
  let component: BannerPrioridad;
  let fixture: ComponentFixture<BannerPrioridad>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [BannerPrioridad],
    }).compileComponents();

    fixture = TestBed.createComponent(BannerPrioridad);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
