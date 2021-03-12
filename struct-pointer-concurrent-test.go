package main

import (
	"encoding/json"
	"fmt"
	"time"
)

// Output is ...
type Output struct {
	Idx int         `json:"idx"`
	Res []Responses `json:"response"`
}

// Responses is ...
type Responses struct {
	Idx int `json:"oid"`
}

func (r *Output) setResponses(res []Responses) {
	r.Res = res
}

func (r *Output) getOutputJSON() string {
	resJSON, _ := json.Marshal(r)
	return string(resJSON)
}

func main() {

	// go func() {
	for count := 0; count <= 10; count++ {
		go api(count)
	}
	// }()

	time.Sleep(time.Second * 6)
}

func api(idx int) {
	time.Sleep(time.Millisecond * 500)

	output := &Output{}
	output.Idx = idx

	var res []Responses
	res = append(res, Responses{
		Idx: idx,
	})
	output.setResponses(res)

	fmt.Println(time.Now(), "\t\taddr:", &output, "idx:", idx, ">>", output.getOutputJSON())
}
