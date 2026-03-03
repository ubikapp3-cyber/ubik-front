import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditProfileMotel } from './edit-profile-motel';

describe('EditProfileMotel', () => {
  let component: EditProfileMotel;
  let fixture: ComponentFixture<EditProfileMotel>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EditProfileMotel]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EditProfileMotel);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
