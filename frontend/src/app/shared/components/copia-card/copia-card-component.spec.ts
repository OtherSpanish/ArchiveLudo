import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CopiaCardComponent } from './copia-card-component';

describe('CopiaCard', () => {
  let component: CopiaCardComponent;
  let fixture: ComponentFixture<CopiaCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CopiaCardComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(CopiaCardComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
