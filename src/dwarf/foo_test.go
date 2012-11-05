package dwarf

import "testing"

func TestSqrt(t *testing.T) {
	if x, _ := Foo("Brian"); x != "Brian" {
		t.Errorf("expected Brian got %s", x)
	}
}
