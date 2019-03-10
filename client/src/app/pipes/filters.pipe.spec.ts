import { SubStringPipe } from './filters.pipe';

describe('FiltersPipe', () => {
  it('create an instance', () => {
    const pipe = new SubStringPipe();
    expect(pipe).toBeTruthy();
  });
});
