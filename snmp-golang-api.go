package main

import (
	"context"
	"encoding/json"
	"flag"
	"fmt"
	"log"
	"net/http"
	"os"
	"os/signal"
	"strconv"
	"strings"
	"syscall"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/gosnmp/gosnmp"
	"github.com/kardianos/service"
	"gopkg.in/natefinch/lumberjack.v2"
)

const logcontextpath = "ismonsnmp"

var (
	logn          = log.Println
	logf          = log.Printf
	prn           = fmt.Println
	prf           = fmt.Printf
	serviceLogger service.Logger
	listenPort    string
)

type program struct{}

// Login is Binding from JSON
type Login struct {
	User     string `form:"user" json:"user" xml:"user"  binding:"required"`
	Password string `form:"password" json:"password" xml:"password" binding:"required"`
}

// APIRequest is Request ...
type APIRequest struct {
	DeviceID     string `json:"deviceid" binding:"required"`
	DeviceIP     string `json:"ip" binding:"required"`
	OidGroupName string `json:"oid-group-name" binding:"required"`
	UserName     string `json:"user" binding:"required"`
	AuthPass     string `json:"authpass" binding:"required"`
	PrivPass     string `json:"privpass" binding:"required"`
	Timeout      int    `json:"timeout" binding:"required"`
	Oid          string `json:"oid" binding:"required"`
	IsYesterday  bool   `json:"is-yesterday"`
}

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

func main() {

	currpath := flag.String("currpath", "H:/shcsw", "program path define")
	port := flag.String("port", "8080", "http listen port")
	flag.Parse()

	if !isFlagInputed("port") {
		flag.Usage()
		return
	}

	if !isFlagInputed("currpath") {
		flag.Usage()
		return
	}

	listenPort = *port

	createDirIfNotExist(*currpath + "/" + logcontextpath + "-logs")

	l := &lumberjack.Logger{
		Filename:   *currpath + "/" + logcontextpath + "-logs" + "/ismonsnmp.log",
		MaxSize:    10, // megabytes
		MaxBackups: 3,
		MaxAge:     28,   //days
		Compress:   true, // disabled by default
	}
	log.SetOutput(l)
	c := make(chan os.Signal, 1)
	signal.Notify(c, syscall.SIGHUP)

	go func() {
		for {
			<-c
			l.Rotate()
		}
	}()

	// * For Service
	svcConfig := &service.Config{
		Name:        "ismonsnmpgo",
		DisplayName: "ismonsnmpgo",
		Description: "Hansol Inticube IS-MON's SNMP Polling Service",
	}

	prg := &program{}
	s, err := service.New(prg, svcConfig)
	if err != nil {
		log.Fatal(err)
	}

	serviceLogger, err = s.Logger(nil)
	if err != nil {
		log.Fatal(err)
	}

	err = s.Run()
	if err != nil {
		serviceLogger.Error(err)
	}

}

// * For Service Start ----------------------------------------------------------------------------------------

func (p *program) Start(s service.Service) error {
	// Start should not block. Do the actual work async.
	go p.run()
	return nil
}

func (p *program) Stop(s service.Service) error {
	// Stop should not block. Return with a few seconds.
	logn("Stop Service.... service name:", s.String())
	prn("Stop Service.... service name:", s.String())
	return nil
}

func (p *program) run() {

	// Disable Console Color, you don't need console color when writing the logs to file.
	gin.DisableConsoleColor()

	// Logging to a file.
	// f, _ := os.Create("gin.log")
	// gin.DefaultWriter = io.MultiWriter(f)

	router := gin.Default()
	router.GET("/ping", func(c *gin.Context) {
		c.JSON(200, gin.H{
			"message": "pong",
		})
	})

	// https://gin-gonic.com/docs/examples/binding-and-validation/
	// Example for binding JSON ({"user": "manu", "password": "123"})
	// $ curl -v -X POST \
	// 	http://localhost:8080/loginJSON \
	// 	-H 'content-type: application/json' \
	// 	-d '{ "user": "manu" }'
	router.POST("/loginJSON", func(c *gin.Context) {
		var json Login
		if err := c.ShouldBindJSON(&json); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}

		if json.User != "manu" || json.Password != "123" {
			c.JSON(http.StatusUnauthorized, gin.H{"status": "unauthorized"})
			return
		}

		c.JSON(http.StatusOK, gin.H{"status": "you are logged in"})
	})

	// https://gin-gonic.com/docs/examples/goroutines-inside-a-middleware/
	router.GET("/long_async", func(c *gin.Context) {
		// create copy to be used inside the goroutine
		cCp := c.Copy()
		go func() {
			// simulate a long task with time.Sleep(). 5 seconds
			time.Sleep(5 * time.Second)

			// note that you are using the copied context "cCp", IMPORTANT
			log.Println("Done! in path " + cCp.Request.URL.Path)

			// NOT WORKING...
			// c.JSON(http.StatusOK, gin.H{"status": "long_async response"})
		}()
	})

	router.GET("/long_sync", func(c *gin.Context) {
		// simulate a long task with time.Sleep(). 5 seconds
		time.Sleep(5 * time.Second)

		// since we are NOT using a goroutine, we do not have to copy the context
		log.Println("Done! in path " + c.Request.URL.Path)

		c.JSON(http.StatusOK, gin.H{"status": "long_sync response"})
	})

	router.POST("/api-snmp", func(c *gin.Context) {
		var req APIRequest
		if err := c.ShouldBindJSON(&req); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{
				"error":          err.Error(),
				"success":        false,
				"deviceid":       req.DeviceID,
				"deviceip":       req.DeviceIP,
				"oid-group-name": req.OidGroupName,
				"is-yesterday":   req.IsYesterday,
				"response":       nil,
			})
			return
		}

		reqJSON, _ := json.Marshal(req)
		logn("request trace :", string(reqJSON))

		// exit := make(chan os.Signal)
		// signal.Notify(exit, syscall.SIGINT, syscall.SIGTERM)

		timeoutSecDuration := strconv.Itoa(req.Timeout+1) + "s"
		maxDuration, _ := time.ParseDuration(timeoutSecDuration)
		ctx, _ := context.WithTimeout(context.Background(), maxDuration)

		// go func() {
		// 	// fmt.Println("Signal:", <-exit)
		// 	cancel()
		// }()

		start := time.Now()
		result, err := snmpWithCtxWrapper(ctx, req)
		logf("duration:%v result:%s\n", time.Since(start), result)

		if err != nil {
			logn(err)
			c.JSON(http.StatusRequestTimeout, gin.H{
				"error":          err.Error(),
				"success":        false,
				"deviceid":       req.DeviceID,
				"deviceip":       req.DeviceIP,
				"oid-group-name": req.OidGroupName,
				"is-yesterday":   req.IsYesterday,
				"response":       nil,
			})
			return
		}
		// {
		//   "success": true,
		//   "deviceid": "DVMGRAP1",
		//   "deviceip": "10.1.15.227",
		//   "oid-group-name": "avCmServerInfo",
		//   "is-yesterday": false,
		//   "elapsedtime": "5.6285ms",
		//   "response": []
		// }
		// c.JSON(http.StatusOK, gin.H{"result": "OK"})
		c.JSON(http.StatusOK, result)
	})

	router.Run(":" + listenPort) // listen and serve on 0.0.0.0:8080
}

func snmpWithCtxWrapper(ctx context.Context, req APIRequest) (string, error) {
	done := make(chan string)

	go func() {
		done <- snmpWalkRun(req)
	}()

	select {
	case <-ctx.Done():
		return "Fail", ctx.Err()
	case result := <-done:
		return result, nil
	}
}

func snmpWalkRun(req APIRequest) string {

	_host := req.DeviceIP
	_oids := req.Oid
	_timeout := req.Timeout
	_user := req.UserName
	_authprotocol := "SHA"
	_authpass := req.AuthPass
	_privcypass := req.PrivPass
	_privacyprotocol := "AES"
	_version := "v3"

	timeoutSecDuration := strconv.Itoa(_timeout) + "s"
	duration, _ := time.ParseDuration(timeoutSecDuration)

	target := _host
	oidstr := _oids
	splitedOid := strings.Split(oidstr, "|")

	startTime := time.Now()

	gosnmp.Default.Target = target
	gosnmp.Default.Timeout = duration // time.Duration(*timeout * time.Second) // Timeout better suited to walking
	gosnmp.Default.Retries = 1

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
		log.Println("Only v3, v2c available")
		return "" // TODO
	}

	err := gosnmp.Default.Connect()
	if err != nil {
		logf("Connect err: %v\n", err)
		return "" // TODO
	}
	defer gosnmp.Default.Conn.Close()

	oid := splitedOid[0]

	output := &Output{}

	output.DeviceID = req.DeviceID
	output.DeviceIP = req.DeviceIP
	output.OidGroupName = req.OidGroupName
	output.IsYesterday = req.IsYesterday

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
		logf("Walk Error: %v\n", errorB)
		output.Success = false
	} else {
		output.Success = true
	}

	unitElapsedTime := time.Since(startTime)
	output.ElabsedTime = unitElapsedTime.String()
	output.setResponses(res)

	return output.getOutputJSON()
}

func createDirIfNotExist(dir string) {
	if _, err := os.Stat(dir); os.IsNotExist(err) {
		err = os.MkdirAll(dir, 0755)
		if err != nil {
			logn(err.Error())
			panic(err)
		}
	}
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
