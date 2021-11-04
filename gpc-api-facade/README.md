# GPC API Facade

## Overview
GPC API Facade - HTTP FHIR API that is similar to GPC 1.6.0 standard.
It will enable NME to request and fetch patient data in the FHIR format.
This facade will only serve light tasks, delegating all the work to the async GP2GP Translator component.