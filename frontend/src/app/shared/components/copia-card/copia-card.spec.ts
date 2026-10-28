import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CopiaCard } from './copia-card';

describe('CopiaCard', () => {
  let component: CopiaCard;
  let fixture: ComponentFixture<CopiaCard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CopiaCard],
    }).compileComponents();

    fixture = TestBed.createComponent(CopiaCard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
