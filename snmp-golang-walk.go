package main

import (
	"context"
	"encoding/json"
	"flag"
	"fmt"
	"log"
	"os"
	"os/signal"
	"strconv"
	"strings"
	"syscall"
	"time"

	"github.com/soniah/gosnmp"
)

// Output is ...
type Output struct {
	Success      bool        `json:"success"`
	DeviceID     string      `json:"deviceid"`
	DeviceIP     string      `json:"deviceip"`
	OidGroupName string      `json:"oid-group-name"`
	IsYesterday  bool        `json:"is-yesterday"`
	ElabsedTime  string      `json:"elapsedtime"`
	Res          []Responses `json:"response"`
}

// Responses is ...
type Responses struct {
	Oid   string `json:"oid"`
	Value string `json:"value"`
}

func (r *Output) setResponses(res []Responses) {
	r.Res = res
}

func (r *Output) getOutputJSON() string {
	resJSON, _ := json.Marshal(r)
	return string(resJSON)
}

var (
	_host            string
	_oids            string
	_timeout         int
	_user            string
	_authprotocol    string
	_authpass        string
	_privcypass      string
	_privacyprotocol string
	_version         string
	_help            bool
)

func main() {

	flhost := flag.String("host", "", "target server ipaddress")
	floids := flag.String("oids", ".1.3.6.1.2.1.1", "input one or multi oid (delimiter : | <-- double quotation need..)")
	fltimeout := flag.Int("timeout", 10, "timeout second (this value is gosnmp.Timeout, program deadline timeout is +1 sec.)")
	fluser := flag.String("user", "ismon", "if v3, username or if v2c, communityname")
	flauthprotocol := flag.String("authprotocol", "MD5", "snmpv3 authentication protocol (MD5, SHA)")
	flauthpass := flag.String("authpass", "ismon123!", "snmpv3 authentication passphrase")
	flprivcypass := flag.String("privacypass", "ismon123!", "snmpv3 privacy passphrase")
	flprivacyprotocol := flag.String("privacyprotocol", "AES", "snmpv3 privacy protocol")
	flversion := flag.String("version", "v3", "SNMP protocol version (v3, v2c available)")
	flhelp := flag.Bool("help", false, "ismonsnmpwalk example content print")
	// _isSnmpGet := flag.Bool("snmpget", false, "snmpget, not bulkwalk...")

	flag.Parse()

	if *flhelp {
		flag.Usage()
		fmt.Println(helpContent)
		return
	}

	if *flhost == "" {
		flag.Usage()
		return
	}

	_host = *flhost
	_oids = *floids
	_timeout = *fltimeout
	_user = *fluser
	_authprotocol = *flauthprotocol
	_authpass = *flauthpass
	_privcypass = *flprivcypass
	_privacyprotocol = *flprivacyprotocol
	_version = *flversion
	_help = *flhelp

	exit := make(chan os.Signal)
	signal.Notify(exit, syscall.SIGINT, syscall.SIGTERM)

	timeoutSecDuration := strconv.Itoa(_timeout+1) + "s"
	maxDuration, _ := time.ParseDuration(timeoutSecDuration)
	ctx, cancel := context.WithTimeout(context.Background(), maxDuration)

	go func() {
		fmt.Println("Signal:", <-exit)
		cancel()
	}()

	start := time.Now()
	result, err := longFuncWithCtx(ctx)
	fmt.Printf("duration:%v result:%s\n", time.Since(start), result)
	if err != nil {
		log.Fatal(err)
	}
}

func longFuncWithCtx(ctx context.Context) (string, error) {
	done := make(chan string)

	go func() {
		done <- snmpWalk()
	}()

	select {
	case <-ctx.Done():
		return "Fail", ctx.Err()
	case result := <-done:
		return result, nil
	}
}

const helpContent = `
example :

- 232 server
./ismonsnmpwalkUnit -host=10.1.12.232 -oids=".1.3.6.1.2.1.25.2.3.1|.1.3.6.1.2.1.25.4.2.1.2|.1.3.6.1.2.1.25.5.1.1.2|.1.3.6.1.2.1.25.3.3.1.2|.1.3.6.1.4.1.2021.9.1.7.1" -timeout=10 -user=ismon -authprotocol=MD5 -authpass=ismon123!

- Avaya CM 8.0
./ismonsnmpwalkUnit -host=10.1.15.227 -oids=".1.3.6.1.4.1.6889.2.73.8.1.33.1.1.3|.1.3.6.1.4.1.6889.2.73.8.1.33.1.1.15|.1.3.6.1.4.1.6889.2.73.8.1.5|.1.3.6.1.4.1.6889.2.73.8.1.1|.1.3.6.1.4.1.6889.2.73.8.1.4|.1.3.6.1.4.1.6889.2.73.8.1.29.14|.1.3.6.1.4.1.6889.2.73.8.1.45|.1.3.6.1.4.1.6889.2.73.8.1.46|.1.3.6.1.4.1.6889.2.73.8.1.8|.1.3.6.1.4.1.6889.2.73.8.1.86|.1.3.6.1.4.1.6889.2.73.8.1.58" -timeout=10 -user=shciptsnmp1 -authprotocol=SHA -authpass=ismon123\!\@ -privacyprotocol=AES -privacypass=ismon123\!\@
./ismonsnmpwalkUnit -host=10.1.15.227 -oids=".1.3.6.1.4.1.6889.2.73.8.1.33.1.1.3" -timeout=10 -user=shciptsnmp1 -authprotocol=SHA -authpass=ismon123\!\@ -privacyprotocol=AES -privacypass=ismon123\!\@

- v2c
./ismonsnmpwalkUnit -host=10.1.12.232 -user=public -version=v2c

- v1
./ismonsnmpwalkUnit -host=10.1.12.70 -user=hcheck -version=v1

- Origin command (Linux)
snmpwalk -v3 -l authPriv -u shciptsnmp1 -a SHA -A ismon123\!\@ -x AES -X ismon123\!\@ 10.1.15.227 .1.3.6.1.4.1.6889.2.73.8.1.5
`

//######################################################################################################
// example :
//
// 232 테스트 서버
// ./ismonsnmpwalkUnit -host=10.1.12.232 -oids=".1.3.6.1.2.1.25.2.3.1|.1.3.6.1.2.1.25.4.2.1.2|.1.3.6.1.2.1.25.5.1.1.2|.1.3.6.1.2.1.25.3.3.1.2|.1.3.6.1.4.1.2021.9.1.7.1" -timeout=10 -user=ismon -authprotocol=MD5 -authpass=ismon123!
//
// 본사 테스트 교환기 Avaya CM 8.0
// ./ismonsnmpwalkUnit -host=10.1.15.227 -oids=".1.3.6.1.4.1.6889.2.73.8.1.33.1.1.3|.1.3.6.1.4.1.6889.2.73.8.1.33.1.1.15|.1.3.6.1.4.1.6889.2.73.8.1.5|.1.3.6.1.4.1.6889.2.73.8.1.1|.1.3.6.1.4.1.6889.2.73.8.1.4|.1.3.6.1.4.1.6889.2.73.8.1.29.14|.1.3.6.1.4.1.6889.2.73.8.1.45|.1.3.6.1.4.1.6889.2.73.8.1.46|.1.3.6.1.4.1.6889.2.73.8.1.8|.1.3.6.1.4.1.6889.2.73.8.1.86|.1.3.6.1.4.1.6889.2.73.8.1.58" -timeout=10 -user=shciptsnmp1 -authprotocol=SHA -authpass=ismon123\!\@ -privacyprotocol=AES -privacypass=ismon123\!\@
//
// v2c
// ./ismonsnmpwalkUnit -host=10.1.12.232 -user=public -version=v2c
//
// Origin command (Linux)
// snmpwalk -v3 -l authPriv -u shciptsnmp1 -a SHA -A ismon123\!\@ -x AES -X ismon123\!\@ 10.1.15.227 .1.3.6.1.4.1.6889.2.73.8.1.5
//######################################################################################################
func snmpWalk() string {

	timeoutSecDuration := strconv.Itoa(_timeout) + "s"
	duration, _ := time.ParseDuration(timeoutSecDuration)

	target := _host
	oidstr := _oids
	splitedOid := strings.Split(oidstr, "|")

	startTime := time.Now()

	gosnmp.Default.Target = target
	// gosnmp.Default.Community = community

	// #### Avaya의 모든 mib들의 권고 타임아웃이 15초 이상이므로 --> 공히 10초로 통일하자 !!!!
	gosnmp.Default.Timeout = duration // time.Duration(*timeout * time.Second) // Timeout better suited to walking

	if _version == "v3" {

		var authProtocol gosnmp.SnmpV3AuthProtocol
		switch _authprotocol {
		case "SHA":
			authProtocol = gosnmp.SHA
		default:
			authProtocol = gosnmp.MD5
		}

		var privacyProtocol gosnmp.SnmpV3PrivProtocol
		switch _privacyprotocol {
		case "DES":
			privacyProtocol = gosnmp.DES
		case "AES":
			privacyProtocol = gosnmp.AES
		case "AES192":
			privacyProtocol = gosnmp.AES192
		case "AES256":
			privacyProtocol = gosnmp.AES256
		default:
			privacyProtocol = gosnmp.AES
		}

		gosnmp.Default.Version = gosnmp.Version3
		gosnmp.Default.SecurityModel = gosnmp.UserSecurityModel
		gosnmp.Default.MsgFlags = gosnmp.AuthPriv
		gosnmp.Default.SecurityParameters = &gosnmp.UsmSecurityParameters{
			UserName:                 _user,
			AuthenticationProtocol:   authProtocol,
			AuthenticationPassphrase: _authpass,
			PrivacyProtocol:          privacyProtocol,
			PrivacyPassphrase:        _privcypass,
		}
	} else if _version == "v2c" {
		gosnmp.Default.Version = gosnmp.Version2c
		gosnmp.Default.Community = _user
	} else if _version == "v1" {
		gosnmp.Default.Version = gosnmp.Version1
		gosnmp.Default.Community = _user
	} else {
		fmt.Println("Only v3, v2c available")
		return "" // TODO
	}

	err := gosnmp.Default.Connect()
	if err != nil {
		fmt.Printf("Connect err: %v\n", err)
		return "" // TODO
	}
	defer gosnmp.Default.Conn.Close()

	// fmt.Println("# hansol inticube all right reserved. by pjw")

	// Get Test --------------------------------------------------------------------------------------------------
	// getOids := []string{".1.3.6.1.4.1.6889.2.73.8.1.8.1.0", ".1.3.6.1.4.1.6889.2.73.8.1.8.2.0"}
	// getResult, getErr := gosnmp.Default.Get(getOids)
	// if getErr != nil {
	// 	fmt.Printf("get error : %v\n", getErr)
	// 	os.Exit(1)
	// }

	// for i, variable := range getResult.Variables {
	// 	fmt.Printf("%d: oid: %s ", i, variable.Name)

	// 	switch variable.Type {
	// 	case gosnmp.OctetString:
	// 		fmt.Printf("[GET] string: %s\n", string(variable.Value.([]byte)))
	// 	default:
	// 		fmt.Printf("[GET] number: %d\n", gosnmp.ToBigInt(variable.Value))
	// 	}
	// }

	// Set Test --------------------------------------------------------------------------------------------------
	// if isFlagInputed("rw1") && isFlagInputed("rw2") {
	// fmt.Println("rw action ------------")
	// pdu1 := gosnmp.SnmpPDU{
	// 	Name:  ".1.3.6.1.4.1.6889.2.73.8.1.8.2.0",
	// 	Type:  gosnmp.OctetString,
	// 	Value: "10", // *_rw1,
	// }

	// pdu2 := gosnmp.SnmpPDU{
	// 	Name:  ".1.3.6.1.4.1.6889.2.73.8.1.8.1.0",
	// 	Type:  gosnmp.OctetString,
	// 	Value: "0", // *_rw2,
	// }
	// setResult, setErr := gosnmp.Default.Set([]gosnmp.SnmpPDU{pdu1, pdu2})
	// if setErr != nil {
	// 	fmt.Printf("set error : %v\n", setErr)
	// 	os.Exit(1)
	// }
	// fmt.Println(setResult)
	// }

	// if !*_isSnmpGet {
	// for x := 0; x < len(splitedOid); x++ {
	oid := splitedOid[0]
	// fmt.Println(oid, "---------------------------------------------------")

	output := &Output{}

	output.DeviceID = "DVMGRAP1"
	output.DeviceIP = "127.0.0.1"
	output.OidGroupName = "avCmServerinfo"
	output.IsYesterday = false

	var res []Responses

	asyncRes := func(pdu gosnmp.SnmpPDU) error {
		// fmt.Printf("%s = ", pdu.Name)

		var value string
		switch pdu.Type {
		case gosnmp.OctetString:
			b := pdu.Value.([]byte)
			// fmt.Printf("STRING: %s\n", string(b))
			value = string(b)
		default:
			// fmt.Printf("TYPE %d: %d\n", pdu.Type, gosnmp.ToBigInt(pdu.Value))
			value = strconv.Itoa(int(gosnmp.ToBigInt(pdu.Value).Int64()))
		}

		res = append(res, Responses{
			Oid:   pdu.Name,
			Value: value,
		})

		return nil
	}

	errorB := gosnmp.Default.BulkWalk(oid, asyncRes)
	if errorB != nil {
		fmt.Printf("Walk Error: %v\n", errorB)
		output.Success = false
	} else {
		output.Success = true
	}

	unitElapsedTime := time.Since(startTime)
	output.ElabsedTime = unitElapsedTime.String()
	output.setResponses(res)

	// fmt.Println(output.getOutputJSON())
	// }

	// elapsedTime := time.Since(startTime)
	// fmt.Printf("# elapsed: %s\n", elapsedTime)

	return output.getOutputJSON()

	// }
	// else {
	// 	// getOids := []string{".1.3.6.1.4.1.6889.2.73.8.1.8.1.0", ".1.3.6.1.4.1.6889.2.73.8.1.8.2.0"}
	// 	getResult, getErr := gosnmp.Default.Get(splitedOid)
	// 	if getErr != nil {
	// 		fmt.Printf("get error : %v\n", getErr)
	// 		os.Exit(1)
	// 	}

	// 	for i, variable := range getResult.Variables {
	// 		fmt.Printf("%d: oid: %s ", i, variable.Name)

	// 		switch variable.Type {
	// 		case gosnmp.OctetString:
	// 			fmt.Printf("[GET] string: %s\n", string(variable.Value.([]byte)))
	// 		default:
	// 			fmt.Printf("[GET] number: %d\n", gosnmp.ToBigInt(variable.Value))
	// 		}
	// 	}
	// }
}

func isFlagInputed(name string) bool {
	found := false
	flag.Visit(func(f *flag.Flag) {
		if f.Name == name {
			found = true
		}
	})
	return found
}
