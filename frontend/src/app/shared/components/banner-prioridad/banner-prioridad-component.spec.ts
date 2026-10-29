import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BannerPrioridadComponent } from './banner-prioridad-component';

describe('BannerPrioridad', () => {
  let component: BannerPrioridadComponent;
  let fixture: ComponentFixture<BannerPrioridadComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [BannerPrioridadComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(BannerPrioridadComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
