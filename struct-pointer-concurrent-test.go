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
// pjw@LAPTOP-IRQOLSO6:~/go-work/ismon-agent$ go run concurrtest.go 
// 2021-03-12 13:59:01.7235351 +0900 KST m=+0.513398401            addr: 0xc0000b4020 idx: 8 >> {"idx":8,"response":[{"oid":8}]}
// 2021-03-12 13:59:01.723248 +0900 KST m=+0.513111501             addr: 0xc00009e020 idx: 4 >> {"idx":4,"response":[{"oid":4}]}
// 2021-03-12 13:59:01.7235245 +0900 KST m=+0.513387801            addr: 0xc0000b4018 idx: 7 >> {"idx":7,"response":[{"oid":7}]}
// 2021-03-12 13:59:01.7237049 +0900 KST m=+0.513568101            addr: 0xc00009e0a0 idx: 0 >> {"idx":0,"response":[{"oid":0}]}
// 2021-03-12 13:59:01.7237308 +0900 KST m=+0.513594601            addr: 0xc00009e0a8 idx: 1 >> {"idx":1,"response":[{"oid":1}]}
// 2021-03-12 13:59:01.7237441 +0900 KST m=+0.513607401            addr: 0xc00009e0b0 idx: 2 >> {"idx":2,"response":[{"oid":2}]}
// 2021-03-12 13:59:01.7237623 +0900 KST m=+0.513625401            addr: 0xc00009e0b8 idx: 3 >> {"idx":3,"response":[{"oid":3}]}
// 2021-03-12 13:59:01.7234488 +0900 KST m=+0.513312101            addr: 0xc0000b4000 idx: 10 >> {"idx":10,"response":[{"oid":10}]}
// 2021-03-12 13:59:01.7234994 +0900 KST m=+0.513362501            addr: 0xc0000b4008 idx: 5 >> {"idx":5,"response":[{"oid":5}]}
// 2021-03-12 13:59:01.7235132 +0900 KST m=+0.513376301            addr: 0xc0000b4010 idx: 6 >> {"idx":6,"response":[{"oid":6}]}
// 2021-03-12 13:59:01.7239451 +0900 KST m=+0.513808501            addr: 0xc00000e010 idx: 9 >> {"idx":9,"response":[{"oid":9}]}
