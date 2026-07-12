#!/bin/bash

echo "===== Failure Scenarios Testing Script ====="
echo "This script tests the distributed transaction behavior under various failure conditions"
echo ""

API_URL=${1:-"http://localhost:8080/person-management-system/api"}
TEST_FILE=${2:-"test-import-data/valid-import.json"}

echo "API URL: $API_URL"
echo "Test File: $TEST_FILE"
echo ""

if [ ! -f "$TEST_FILE" ]; then
    echo "ERROR: Test file not found: $TEST_FILE"
    exit 1
fi

function test_normal_operation() {
    echo "===== Test 1: Normal Operation (All Services Running) ====="
    echo "Expected: Import should succeed, data saved in both DB and MinIO"
    
    curl -X POST \
        -F "file=@$TEST_FILE" \
        "$API_URL/import/persons?username=test-user-normal" \
        -H "Accept: application/json"
    
    echo -e "\n"
    sleep 2
}

function test_minio_failure() {
    echo "===== Test 2: MinIO Failure (DB Running, MinIO Down) ====="
    echo "Expected: Import should fail, no data in DB (rollback), no file in MinIO"
    echo "Action: Stop MinIO now (docker stop person-management-minio)"
    read -p "Press Enter when MinIO is stopped..."
    
    curl -X POST \
        -F "file=@$TEST_FILE" \
        "$API_URL/import/persons?username=test-user-minio-down" \
        -H "Accept: application/json"
    
    echo -e "\n"
    echo "Action: Start MinIO (docker start person-management-minio)"
    read -p "Press Enter to continue..."
}

function test_database_failure() {
    echo "===== Test 3: Database Failure (MinIO Running, DB Down) ====="
    echo "Expected: Import should fail, no data in DB, no file in MinIO"
    echo "Action: Stop PostgreSQL now (docker stop person-management-postgres)"
    read -p "Press Enter when PostgreSQL is stopped..."
    
    curl -X POST \
        -F "file=@$TEST_FILE" \
        "$API_URL/import/persons?username=test-user-db-down" \
        -H "Accept: application/json"
    
    echo -e "\n"
    echo "Action: Start PostgreSQL (docker start person-management-postgres)"
    read -p "Press Enter to continue..."
}

function test_runtime_exception() {
    echo "===== Test 4: Runtime Exception Simulation ====="
    echo "Expected: Graceful rollback of both DB and MinIO"
    echo "Note: This requires invalid/corrupted JSON to trigger an exception"
    
    echo '{"invalid": "json without required fields"}' > /tmp/invalid-import.json
    
    curl -X POST \
        -F "file=@/tmp/invalid-import.json" \
        "$API_URL/import/persons?username=test-user-exception" \
        -H "Accept: application/json"
    
    rm /tmp/invalid-import.json
    echo -e "\n"
}

function test_concurrent_imports() {
    echo "===== Test 5: Concurrent Import Requests ====="
    echo "Expected: Both imports should succeed without conflicts"
    
    echo "Starting concurrent imports..."
    curl -X POST -F "file=@$TEST_FILE" "$API_URL/import/persons?username=concurrent-user-1" -H "Accept: application/json" &
    PID1=$!
    
    curl -X POST -F "file=@$TEST_FILE" "$API_URL/import/persons?username=concurrent-user-2" -H "Accept: application/json" &
    PID2=$!
    
    wait $PID1
    wait $PID2
    
    echo -e "\n"
}

function verify_import_history() {
    echo "===== Verification: Import History ====="
    echo "Checking import history..."
    
    curl -X GET "$API_URL/import/history" -H "Accept: application/json"
    echo -e "\n"
}

if [ "$3" == "auto" ]; then
    echo "Running automated tests (skipping failure scenarios)..."
    test_normal_operation
    test_runtime_exception
    test_concurrent_imports
    verify_import_history
else
    echo "Select test to run:"
    echo "1) Normal Operation"
    echo "2) MinIO Failure"
    echo "3) Database Failure"
    echo "4) Runtime Exception"
    echo "5) Concurrent Imports"
    echo "6) Verify Import History"
    echo "7) Run All Tests (manual)"
    echo ""
    read -p "Enter choice [1-7]: " choice
    
    case $choice in
        1) test_normal_operation ;;
        2) test_minio_failure ;;
        3) test_database_failure ;;
        4) test_runtime_exception ;;
        5) test_concurrent_imports ;;
        6) verify_import_history ;;
        7)
            test_normal_operation
            test_minio_failure
            test_database_failure
            test_runtime_exception
            test_concurrent_imports
            verify_import_history
            ;;
        *) echo "Invalid choice" ;;
    esac
fi

echo ""
echo "===== Testing Complete ====="
echo "Review the results above and check the database/MinIO for consistency"
