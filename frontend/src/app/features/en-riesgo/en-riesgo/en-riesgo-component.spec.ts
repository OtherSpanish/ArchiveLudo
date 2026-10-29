import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EnRiesgoComponent } from './en-riesgo-component';

describe('EnRiesgoComponent', () => {
  let component: EnRiesgoComponent;
  let fixture: ComponentFixture<EnRiesgoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [EnRiesgoComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(EnRiesgoComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
