import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RegistrarCopiaComponent } from './registrar-copia-component';

describe('RegistrarCopiaComponent', () => {
  let component: RegistrarCopiaComponent;
  let fixture: ComponentFixture<RegistrarCopiaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [RegistrarCopiaComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(RegistrarCopiaComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
