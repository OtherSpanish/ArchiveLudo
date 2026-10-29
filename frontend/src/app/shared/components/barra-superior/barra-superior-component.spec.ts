import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BarraSuperiorComponent } from './barra-superior-component';

describe('BarraSuperior', () => {
  let component: BarraSuperiorComponent;
  let fixture: ComponentFixture<BarraSuperiorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [BarraSuperiorComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(BarraSuperiorComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
